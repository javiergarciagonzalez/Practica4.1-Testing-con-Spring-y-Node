const supertest = require('supertest');
const app = require("../../src/app");
const request = supertest(app)
const aws = require('aws-sdk');
jest.mock('aws-sdk');

describe('Film controller unit tests', () => {

    let films = [{name: 'Sound of Metal', score: 7.8},
        {name: 'The Mandalorian', score: 8.8},
        {name: 'Attack on Titan', score: 8.9}]


    beforeAll(() => {
        aws.DynamoDB.DocumentClient.prototype.put.mockImplementation((params, cb) => {
            cb(null, params.Item);
        });

        aws.DynamoDB.DocumentClient.prototype.scan.mockImplementation((params, cb) => {
            cb(null, {Items: films});
        });

    });

    afterAll(() => {
        jest.resetAllMocks();
    });

    test('Insert a film', async () => {

        let film = films[0];
        film.id = 0;
        const response = await request.post('/api/films/')
            .send(film)
            .expect(201)

        expect(response.body.id).toBe(film.id);
        expect(response.body.name).toBe(film.name);
        expect(response.body.score).toBe(film.score);

        expect(aws.DynamoDB.DocumentClient.prototype.put).toBeCalledTimes(1);

    });

    test('Get all films', async () => {
        const response = await request.get('/api/films/').expect(200);

        expect(response.body[0].name).toBe(films[0].name);
        expect(response.body[0].score).toBe(films[0].score);

        expect(response.body[1].name).toBe(films[1].name);
        expect(response.body[1].score).toBe(films[1].score);

        expect(response.body[2].name).toBe(films[2].name);
        expect(response.body[2].score).toBe(films[2].score);

        expect(aws.DynamoDB.DocumentClient.prototype.scan).toBeCalledTimes(1);

    });
});